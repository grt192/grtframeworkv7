#!/bin/sh

#cat <<EOF | ftp ftp://anon:anon@10.1.92.2
echo "Moving files over..."
cat <<EOF | echo
put ${1}.txt /constants.txt 
EOF

#move all the other files over
for i in `ls constants_files/constants_$1 | grep -v \~`; do cat <<EOF | ftp ftp://anon:anon@10.1.92.2
puts $i /constants_${1}/$i
done