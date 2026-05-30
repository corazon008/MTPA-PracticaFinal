# MTPA-PracticaFinal
Corentin CALVEZ - Lubin TERRIEN

## Server launch

The server can be started with the helper script:

```bash
./run-server.sh
```

The script initializes the data/log/metrics directories, then launches the Maven exec entry point.

## Configuration

The server reads an optional properties file. By default it looks for `server.properties` in the project root.
You can also point to another file with:

```bash
SERVER_CONFIG=/path/to/server.properties ./run-server.sh
```

Supported properties:

- `server.port` - listening port for the server
- `persistence.dir` - base directory for JSON persistence files
- `server.log.dir` - directory used for rolling server logs
- `server.log.limit.bytes` - per-log-file size before rollover
- `server.log.file.count` - number of rotated log files to keep
- `server.metrics.file` - output file for the metrics snapshot

## Files

- `run-server.sh` - starts the server through Maven
- `init-server-data.sh` - creates the required data/log/metrics folders
- `server.properties.example` - sample configuration file
