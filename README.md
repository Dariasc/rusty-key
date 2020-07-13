# Rusty Key

Rusty Key is a small android application created with the purpose of remotely opening a Keepass database with biometrics.

<p align="center">
  <img src="https://i.imgur.com/8TzO8of.gif" />
</p>

## Installation

### Android

Install the android application with the apk then configure a host and password.

### Server

To run the server it is as any other node project.

```bash
npm install
node index.js
```

For persistent use I would recommend setting it up with pm2

#### Config

A minimal config is needed to run the server. 

`config.json`
```json
{
  "executable": "path/to/keepass/executable",
  "database": "path/to/keepass/database"
}
```


#### Windows

For pm2 to work correctly on startup on windows use [pm2-windows-startup](https://www.npmjs.com/package/pm2-windows-startup)

## How it works

The lifecycle of the password is as follows:

### Storage
- Password is encrypted with a symmetric key provided by the [android keystore](https://developer.android.com/training/articles/keystore) using `.setUserAuthenticationRequired(true)` it is then stored in shared prefs.

### Remote
- When unlocked the password is decrypted using the same key fetched from the keystore.
- The application fetches the public RSA key being used by the server from the `/key` endpoint.
- Then the password is encrypted with the public key and sent to the server via the `/unlock` endpoint.
- The server decrypts using the private key and feeds it to your Keepass application through stdin. 

I am by no means a security expert, I did it this way because it seemed the best approach without using self-signed certificates.

Only tested with KeepassXC, it may need some modifications to work for other applications.

## Todo

- Handle errors without crashing on the android app
- Prevent a replay attacks via some sort of nonce or timestamp

## Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

Please make sure to update tests as appropriate.

## License
[MIT](https://choosealicense.com/licenses/mit/)
