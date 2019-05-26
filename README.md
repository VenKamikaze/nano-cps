# nano-cps
Nano Node confirmation per second counter

## Overview
This is a spring-boot JAR that runs as a standalone web application.

On hitting the URL /subscribe/confirmation it will connect to the Nano Node websocket port and subscribe to block confirmations. On receipt of a block it will store the unixtime and hash in an in-memory database.

After that point, you can hit the URL /cps to get an up-to-date CPS overview, or specify your own period with /cps/{timeinsec}.

## Running

1) Make sure your node has the websocket port open and enabled (see config.json for your node)
2) Download the release: nano-cps-0.2.jar (or newer if one exists)
3) Download application.properties and edit the websocket address/port if needed.
4) Run the JAR: java -jar nano-cps-0.2.jar
5) After it's finished starting, hit this URL: http://127.0.0.1:8080/subscribe/confirmation
6) Then check: http://127.0.0.1:8080/cps whenever you want to see the CPS rate.

## Warning
Don't run this forever, as it has no mechanism to clean up old records and will eventually chew up a lot of RAM.
