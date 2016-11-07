package toidiu.com.fieldnotebook.utils;

import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;

import org.apache.commons.io.FileUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Route;
import toidiu.com.fieldnotebook.SecretUtils;

public class UtilZamzar {

    public static final int TIMEOUT = 10;
    //region Helper----------------------
    private static OkHttpClient okHttpClient;

    private static OkHttpClient getClientInstance() {
        //------------http://stackoverflow.com/a/34819354/2369122
        if (okHttpClient != null) {
            return okHttpClient;
        }

        OkHttpClient.Builder client = new OkHttpClient.Builder();
        client.authenticator(new Authenticator() {
            @Override
            public Request authenticate(Route route, Response response) throws IOException {
                String credential = Credentials.basic(SecretUtils.apiKey, "");
                return response.request().newBuilder().header("Authorization", credential).build();
            }
        });
        client.connectTimeout(TIMEOUT, TimeUnit.SECONDS);
        client.writeTimeout(TIMEOUT, TimeUnit.SECONDS);
        client.readTimeout(TIMEOUT, TimeUnit.SECONDS);
        okHttpClient = client.build();
        return okHttpClient;
    }
    //endregion

    //region API CALLS----------------------
    public static String info() {
        String endpoint = "https://sandbox.zamzar.com/v1/formats/dwg";
        Request request = new Request.Builder()
                .url(endpoint).get().build();

        Response execute = null;
        try {
            execute = getClientInstance().newCall(request).execute();
            String string = execute.body().string();
            Log.d("info----------", string);
            return string;
        } catch (IOException e) {
            Crashlytics.getInstance().core.logException(e);
            return null;
        }
    }

    public static ConvertResp convert(File sourceFile) {
        String endpoint = "https://sandbox.zamzar.com/v1/jobs";

        String targetFormat = "pdf";

        MediaType MEDIA_TYPE_DWG = MediaType.parse("application/acad");

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("source_file", sourceFile.getName(), RequestBody.create(MEDIA_TYPE_DWG, sourceFile))
                .addFormDataPart("target_format", targetFormat)
                .build();

        Request request = new Request.Builder()
                .url(endpoint).post(requestBody).build();

        Response execute = null;
        try {
            execute = getClientInstance().newCall(request).execute();
            String string = execute.body().string();
            Log.d("convert----------", string);
            ConvertResp convertResp = new Gson().fromJson(string, ConvertResp.class);
            return convertResp;
        } catch (IOException e) {
            Crashlytics.getInstance().core.logException(e);
            return null;
        }
    }

    public static AskResp askStatus(int jobId) {
        String endpoint = "https://sandbox.zamzar.com/v1/jobs/" + jobId;
        Request request = new Request.Builder()
                .url(endpoint).get().build();
        Response execute = null;
        try {
            execute = getClientInstance().newCall(request).execute();
            String string = execute.body().string();
            Log.d("askStatus----------", string);
            AskResp askResp = new Gson().fromJson(string, AskResp.class);
            return askResp;
        } catch (IOException e) {
            Crashlytics.getInstance().core.logException(e);
            return null;
        }
    }

    public static File download(int fileId, final File localFile) {
        String endpoint = "https://sandbox.zamzar.com/v1/files/" + fileId + "/content";

        Request request = new Request.Builder().url(endpoint).get().build();

        try {
            Response execute = getClientInstance().newCall(request).execute();
//            if(execute.code() == 404){
//                return null;
//            }
            BufferedInputStream bis = new BufferedInputStream(execute.body().byteStream());
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(localFile));


//            byte[] buffer = new byte[1024];
//            int len;
//            while ((len = bis.read(buffer)) != -1) {
//                bos.write(buffer, 0, len);
//            }

//            int inByte;
//            while ((inByte = bis.read()) != -1) {
//                bos.write(inByte);
//            }
//            Log.d("download----------", "File downloaded");
//            execute.body().close();
//            bos.close();
//            bis.close();

            FileUtils.copyInputStreamToFile(bis, localFile);
            bis.close();
            return localFile;
        } catch (IOException e) {
            Crashlytics.getInstance().core.logException(e);
            return null;
        }
    }
    //endregion


    public static class ConvertResp {
        public Integer id;
        public String key;
        public String status;
        public Integer credit_cost;
    }

    public static class AskResp {
        public Integer id;
        public String key;
        public String status;
        public TargetFiles[] target_files;
    }

    public static class TargetFiles {
        public Integer id;
        public String name;
    }

}
