const { Sequelize, DataTypes, Op } = require('sequelize');
const sequelize = new Sequelize('database', null, null, {
    dialect: 'sqlite',
    storage: 'database.sqlite',
});


const Availability = sequelize.define('Availability', {
    username: {
        type: DataTypes.STRING,
    },

    start: {
        type: DataTypes.DATE,
        allowNull: false
    },
    end: {
        type: DataTypes.DATE,
        allowNull: false
    }
});

/**
 * 
 * description: get availability windows for a username between given start and end dates
 * 
 */
async function getAvailabilityWindows(username) {
    return await Availability.findAll({
        where: {
            username: username,
            start: {
                [Op.gt]: new Date()
            },
            end: {
                [Op.gt]: new Date()
            }
        }
    })
}

module.exports = { 
    Availability,
    sequelize,
    getAvailabilityWindows
}
