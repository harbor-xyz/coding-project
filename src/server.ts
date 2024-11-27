import express from 'express';
import { SlotsUtilSController } from './controllers/slotsUtils.controller';

const app = express();
import { DatabaseConnection } from './database/dbConnection';
import { UserController } from './controllers/user.contoller';
DatabaseConnection.connect();
const port = 3000;

// JSON body parser
app.use(express.json());

// URL-encoded body parser
app.use(express.urlencoded({ extended: true }));

app.post('/user',  UserController.create);
app.get('/users', UserController.get);

app.get('/:userId/get-slots', SlotsUtilSController.getAvailableSlots);

app.post('/:userId/book-slot', SlotsUtilSController.bookSlot);

app.get('/:userId/get-events', SlotsUtilSController.getEvents);

app.post('/cancel-event/:eventId', SlotsUtilSController.cancelEvent);

app.get('/get-conflicts', SlotsUtilSController.getConflicts);

app.post('/:userId/set-available-slots', SlotsUtilSController.setAvailableSlots);

app.patch(`/:userId/accept-event/:eventId`, SlotsUtilSController.acceptEvent);



app.listen(port, () => {
  console.log(`Server started at http://localhost:${port}`);
});