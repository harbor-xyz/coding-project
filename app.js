const express = require('express')
const { sequelize, Calendar, Users } = require('./models/');

const app = express()
app.use(express.json())

/**
 * Routes for calendly
 * Setting userID initially to insert to database
 * */
app.post('/set/username/:userID', async (req, res) => {
  try {
    await Users.create({ username: req.params.userID })
    res.json({ success: req.params.userID })
  } catch(e) {
    res.json(JSON.stringify(e))
  }
})

/**
 *  Set availability format is
 *  1 is Monday
 *  2 is Tuesday
 *  7 is Sunday
 *  {
      "userID": "username",
      "availability": {
        "duration": 30,
        "fromTime": "2022-05-05T18:51:34.708Z",
        "toTime": "2022-07-05T18:52:45.186Z",
        "skipDays": [1, 2, 3]
      }
    }
 */
app.post('/set/availability', async(req, res) => {
  try {
    await Calendar.create({
      userID: req.body.userID,
      skipDays: req.body.availability.skipDays,
      from: new Date(req.body.availability.fromTime),
      to: new Date(req.body.availability.toTime),
      duration: req.body.availability.duration
    })
    res.json({ success: true })
  } catch(e) {
    console.log(e)
    res.json(JSON.stringify(e))
  }
})


/**
 * Getting userID availability stored in calendar
 * */
app.get('/get/availability/:userID', async (req, res) => {
  try {
    const resp = await Calendar.findByPk(req.params.userID)
    res.json(resp)
  } catch(e) {
    console.log(e)
    res.json(e)
  }
})


module.exports = app;