## 1.3.0
- Updated to minecraft 1.21.0
- Added server running argument
- Improved shutdown behaviour to wait for file handlers to close
- Changed default scripts to always perform backup on server shutdown

## 1.2.1
- Updated to minecraft 1.20.4
- Changed script argument tick time to AVG tick time

## 1.2.0
- Removed waiting for `on-save.sh` at server tick, now script may run as long as server is still running
- Moved scripts directory to `config` directory
- Changed script parameter from TPS to tick time
- Adjusted script output logging, now standard output and standart error are read at the same time, so potential warnings are in correct order of occurrence among normal messages

Thanks @wereii and @NekoNoor for reporting issues. 
## 1.1.0
- Extracted logic to user defined scripts
## 1.0.0
- Initial release