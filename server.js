const app = require('./app')
const { sequelize } = require('./models/');
const port = 3000


app.listen(port, async() => {
  await sequelize.sync()
  console.log(`Example app listening on port ${port}`)
})