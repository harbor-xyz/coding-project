const { listen } = require('express/lib/application');
const { sortDatesCompare, calculateUserAvailabilityOverlap } = require('../helpers/availabilityHelper');
const { getAvailabilityWindows, Availability } = require('../models');

async function setAvailability (req, resp) {
    try {
        const username = req.params.username;

        if (Date.parse(req.query.start) == NaN || Date.parse(req.query.end) == NaN ) {
            resp.status(422);
            return resp.send( "Start date and End date should be in the format YYYY-MM-DD HH:MM:SS");
        }

        if (new Date(req.query.start) > new Date(req.query.end)) {
            resp.status(422);
            return resp.send( "Start date should be less than equal to end date");
        }

        await Availability.create({
            username: username,
            start: req.query.start,
            end: req.query.end,
        })
        return resp.sendStatus(201);
    } catch (e) {
        return resp.send(`Error encountered: ${JSON.stringify(e)}`)
    }
}

async function getAvailability(req, resp) {
    try {
        availabilityWindow = await getAvailabilityWindows(req.params.username)
        return resp.send(availabilityWindow);
    } catch (e) {
        return resp.send(`Error encountered: ${JSON.stringify(e)}`)
    }
}

async function overlapAvailability(req, resp) {
    try {
        userAAvailability =  await getAvailabilityWindows(req.params.username1);
        userBAvailability =  await getAvailabilityWindows(req.params.username2);
        userAAvailability.sort(sortDatesCompare);
        userBAvailability.sort(sortDatesCompare);

        overlapAvailability = calculateUserAvailabilityOverlap(userAAvailability, userBAvailability);
        return resp.send(overlapAvailabilityWindow);
    } catch (e) {
        return resp.send(`Error encountered: ${JSON.stringify(e)}`)
    }
}

module.exports = {
    setAvailability, getAvailability,overlapAvailability
}
