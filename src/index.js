const express = require('express');
const { router } = require('./api/routes');
const { sequelize } = require('./api/models');

const app = express();
const PORT = 3001;

app.use('/api', router);

app.listen(PORT, async () => {
    await sequelize.sync({ alter: true });
    console.log(`server listening on ${PORT}`)
});

module.exports = {
    app
}
