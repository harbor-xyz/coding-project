const express = require('express');

const router = express.Router();
const { getAvailability, setAvailability, overlapAvailability } = require('../controllers/availability')

/**
 * POST /availability/:username
 * summary: set user availability for userID for given time period
 * parameter:
 *   - in: path
 *     name: username
 *     description: User ID of the user availability needs to be set
 *     type: integer
 *     required: true
 *   - in: query
 *     name: start
 *     description: Start time of the availability window
 *     type: timestamp (format: YYYY-MM-DD HH:MM:SS)
 *     required: true 
 *   - in: query
 *     name: end
 *     description: End time of the availability window
 *     type: timestamp (format: YYYY-MM-DD HH:MM:SS)
 *     required: true
 * response:
 *   - 201, if successful
 *
 */


router.route('/availability/:username').post(setAvailability);

/**
 * GET /availability/:username
 * summary: set user availability for userID for given time period
 * parameter:
 *   - in: path
 *     name: username
 *     description: User ID of the user availability needs to be set
 *     type: integer
 *     required: true
 * response:
 *   - List of availability windows for the username
 *
 */

router.route('/availability/:username').get(getAvailability);

/**
 * GET /availability/overlap/:username1/:username2
 * summary: find overlap of availabilities between two users
 * parameter:
 *   - in: path
 *     name: username1
 *     description: User ID of the first user
 *     type: integer
 *     required: true
 *   - in: path
 *     name: username2
 *     description: User ID of the second user
 *     type: integer
 *     required: true
 * response:
 *   - List of overlapping availability windows for both the users
 *
 */

router.route('/availability/overlap/:username1/:username2').get(overlapAvailability);

module.exports = {
    router
}

