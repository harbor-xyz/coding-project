import mongoose from 'mongoose';

export class DatabaseConnection {
    private static instance: DatabaseConnection;

    private constructor() {
        // Private constructor to prevent direct instantiation
    }

    public static getInstance(): DatabaseConnection {
        if (!DatabaseConnection.instance) {
            DatabaseConnection.instance = new DatabaseConnection();
        }
        return DatabaseConnection.instance;
    }

    static async connect() {
        try {
            // Specify the database name explicitly
            const connectionString = 'mongodb+srv://public:publicpass1234@cluster0.xevrr.mongodb.net/calendly';

            await mongoose.connect(connectionString, {
                // For newer Mongoose versions, use these options
                serverSelectionTimeoutMS: 5000,
                socketTimeoutMS: 45000,
                family: 4 // Use IPv4, skip trying IPv6
            });

            console.log('Successfully connected to MongoDB');
            return this;
        } catch (error) {
            console.error('MongoDB connection error:', error);
            throw error;
        }
    }

    static async disconnect() {
        try {
            await mongoose.disconnect();
            console.log('Disconnected from MongoDB');
        } catch (error) {
            console.error('Error disconnecting from MongoDB:', error);
        }
    }
}