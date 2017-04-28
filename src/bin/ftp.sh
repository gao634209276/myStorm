#!/usr/bin/env bash
echo "this is a test"

host=""
user=""
passwd=""
local=/home/noah/templates/unsm/smslogs/
path=/disk/ftp/YH_FTP/test
file=MOB-UPAY-MIN
 ftp -n -i -v ${host} << EOF
	user ${user} ${passwd}
	binary
	lcd ${local}
	cd ${path}
	mput *${file}*
	bye
EOF
