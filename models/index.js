const { Sequelize, Model, DataTypes } = require("sequelize");

// Passing sqlite
const sequelize = new Sequelize('calendly', null, null, {
  dialect: "postgres",
});

const Calendar = sequelize.define('Calendar', {
  userID: { type: DataTypes.STRING, primaryKey: true },
  skipDays: { type: DataTypes.ARRAY(DataTypes.INTEGER) },
  from: { type: DataTypes.DATE },
  to: { type: DataTypes.DATE },
  duration: { type: DataTypes.INTEGER }
});

const Users = sequelize.define('User', {
  username: { type: DataTypes.STRING, primaryKey: true },
});

module.exports = { sequelize, Calendar, Users };
