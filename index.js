const express = require('express')
const bodyParser = require('body-parser')
const morgan = require('morgan')

const { execFile } = require('child_process')
const config = require('./config.json')

const NodeRSA = require('node-rsa')

const key = new NodeRSA({b: 512});
key.setOptions({
  encryptionScheme: 'pkcs1'
})
key.generateKeyPair()

console.log('[+] generated RSA key pair')
console.log(key.exportKey('pkcs8-public'))


const app = express()
const port = 41337

app.use(morgan('dev'))
app.use(bodyParser.text())

app.post('/unlock', (req, res) => {
  let decrypted = key.decrypt(Buffer.from(req.body, 'base64'))
  console.log(`password: ${decrypted}`)
  let keepass = execFile(config.executable, ["--pw-stdin", config.database])
  keepass.stdin.write(`${decrypted}\n`)
  res.send()
})

app.get('/key', (req, res) => {
  res.send(key.exportKey('pkcs8-public'))
})

app.listen(port, () => console.log(`[+] listening at http://localhost:${port}`))
