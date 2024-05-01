#!/bin/sh

# /etc/X11/xinit/xinitrc
#
# global xinitrc file, used by all X sessions started by xinit (startx)

# invoke global X session script
#. /etc/X11/Xsession

# MPTV
# unclutter to remove cursor pointer appearing on screen
unclutter -idle 0 &
/home/iptv/mptv/mptv.sh
