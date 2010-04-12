#!/bin/sh
knrun=`ps U k3rnel | grep -v grep | grep KNServer.jar`
        if [ "$knrun" = "" ]; then
        echo "Starting K3RNEL Server..." 
        java -Xmx956m -jar /home/k3rnel/Server/KNServer.jar -s low -p 100 --nogui --autorun > server.log &
        echo "K3RNEL Server started!!!"
        else
        echo "K3RNEL Server was already running!"
    fi

