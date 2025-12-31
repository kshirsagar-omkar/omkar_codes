#!/bin/bash


while true
do
    echo "1 Create File"
    echo "2 Delete File"
    echo "3 Backup Directory"
    echo "4 Check Service Status"
    echo "5 Exit"
    read -p "Enter your choice: " choice
    
    case $choice in 
        1)
            read -p "Enter file name to create: " fname
            touch "$fname"
            echo "File '$fname' created."
            ;;
        2)
            read -p "Enter file name to delete: " fname
            if [ -f "$fname" ]; then
                rm "$fname"
                echo "File '$fname' deleted."
            else
                echo "File '$fname' does not exist."
            fi
            ;;
        3)
            read -p "Enter directory to backup: " dir
            read -p "Enter backup destination: " dest
            if [ -d "$dir" ]; then
                tar -czf "$dest/backup_$(basename $dir).tar.gz" "$dir"
                echo "Directory '$dir' backed up to '$dest'."
            else
                echo "Directory '$dir' does not exist."
            fi
            ;;
        4)
            echo "Available services:"
            service --status-all
            echo ""
            read -p "Enter service name to check status (or press Enter to skip): " service
            if [ -n "$service" ]; then
                service "$service" status
            fi
            ;;
        5)
            echo "Exiting..."
            break
            ;;
        *)
            echo "Invalid choice. Please try again."
            ;;
    esac
    echo ""
done