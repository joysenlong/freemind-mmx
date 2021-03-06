#! /bin/zsh

PATH=/bin:/sbin:/usr/bin:/usr/sbin
SCRATCH=/tmp/.mkdmg.$$

# Output
#
croak()
{
    echo -n "\n$1"
}

# Clean up
#
halt()
{
    rm -rf $SCRATCH
    # defaults write com.apple.finder ShowRemovableMediaOnDesktop 1
    # chkerror
    # FINDERPID=`ps -auxwww | grep Finder.app | grep -v grep | awk '{print $2}'`
    # chkerror
    # kill -HUP $FINDERPID 2>/dev/null >/dev/null
    # chkerror
    exit 1
}

# Check return status and bail out on error
#
chkerror()
{
    if [ $? -ne 0 ]
    then
        halt
    fi
}

main()
{

    # Check if exactly one command line argument was specified
    #
    if [ $ARGC -ne 1 ]
    then
        echo "usage: mkdmg <file|directory>"
        exit 1
    fi

    # Check if the specified file/directory exists
    #
    if [ ! -e $1 ]
    then
        echo "*** $1 does not exist."
        exit 1
    fi

    SRC=$1
    NAME=`basename $SRC`
    NAME="$NAME"
    ARCH="$NAME Archive"

    echo -n "Using source $SRC"

    # Change directory to a scratch location
    #
    cd /tmp

    # Create a scratch directory
    #
    mkdir $SCRATCH
    croak "Creating temporary directory $SCRATCH"

    # Estimate how much space is needed to archive the file/folder
    #
    SIZE=`du -s -k $SRC | awk '{print $1}'`
    chkerror
    SIZE=`expr 5 + $SIZE / 1000`
    chkerror
    croak "Using $SIZE MB"

    # Create a disk image, redirecting all output to /dev/null
    #
    hdiutil create "$SCRATCH/$ARCH.dmg" -volname "$ARCH" -megabytes $SIZE -type SPARSE -fs HFS+ 2>/dev/null >/dev/null
    chkerror
    croak "$SCRATCH/$ARCH.dmg created"

    # Optionally disable display of removable media on Desktop
    # 
    # defaults write com.apple.finder ShowRemovableMediaOnDesktop 0
    # chkerror
    # FINDERPID=`ps -auxwww | grep Finder.app | grep -v grep | awk '{print $2}'`
    # chkerror
    # kill -HUP $FINDERPID 2>/dev/null >/dev/null
    # chkerror
    #

    # Mount sparse image
    #
    hdid "$SCRATCH/$ARCH.dmg.sparseimage" 2>/dev/null >/dev/null
    chkerror
    croak "$SCRATCH/$ARCH.dmg.sparseimage attached"
   
    # Find out allocated device
    #
    DEV=`mount | grep "Volumes/$ARCH" | awk '{print $1}'`
    croak "Device in use is $DEV"

    # Use ditto to copy everything to the image, preserving resource forks
    #
    ditto -rsrcFork $SRC "/Volumes/$ARCH/" 2>/dev/null >/dev/null
    chkerror
    croak "Copied $SRC to /Volumes/$ARCH/"

    # Detach the disk image
    hdiutil detach $DEV 2>/dev/null >/dev/null
    chkerror
    croak "$DEV detached"

    # Compress the image (maximum compression)
    hdiutil convert "$SCRATCH/$ARCH.dmg.sparseimage" -format UDZO -o "/tmp/$ARCH.dmg" -imagekey zlib-devel=9 2>/dev/null >/dev/null
    chkerror
    croak "Disk image successfully compressed"

    croak "/tmp/$ARCH.dmg is ready"

    echo

    halt
}

main $1
