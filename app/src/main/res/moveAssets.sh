# 1 is source dir :  path to res 1
# 2 is destination dir  : path to res folder in proj
# 3 is y to excute  : y to confirm that we should do this
# example:   ./moveAssets.sh ~/Downloads/res8/ ./ y
echo "$1------------------"
echo "$2------------------"
echo "$3------------------"



hdpi="drawable-hdpi"
mdpi="drawable-mdpi"
xdpi="drawable-xhdpi"
xxdpi="drawable-xxhdpi"
xxxdpi="drawable-xxxhdpi"

src="$1"
dst="$2"

echo $src
echo $dst

if [ "$3" = "y" ]; then
  echo yes
  cp $src/$hdpi/* $dst/$hdpi
  cp $src/$mdpi/* $dst/$mdpi
  cp $src/$xdpi/* $dst/$xdpi
  cp $src/$xxdpi/* $dst/$xxdpi
  cp $src/$xxxdpi/* $dst/$xxxdpi
fi
