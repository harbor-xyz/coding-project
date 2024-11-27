import { CreateUserLogic } from "../logics/createUser.logic";
import { Request, Response } from "express";
import { GetAllUsersLogic } from "../logics/getAllUsers.logic";

export class UserController {

    static async create(req: Request, res: Response) {
        const responseObj = {
            message: "Successfully created user",
            data: {},
            meta: {
                success: true
            }
        }
        try {
            const { name, email } = req.body;
            if(!name || !email) {
                throw { status: 400, message: "Please provide a name and email" };
            }
            const createUserLogic = new CreateUserLogic(name, email);
            await createUserLogic.execute();
            res.status(201).json(responseObj);
        } catch(e) {
            const status = e.status || 500;
            responseObj.message = e.message || "Internal server error";
            responseObj.meta.success = false;
            res.status(status).json(responseObj);
        }
    }

    static async get(req: Request, res: Response) {
        const responseObj = {
            message: "Successfully fetched users",
            data: {},
            meta: {
                success: true
            }
        }
        try {
            const { search } = req.query;
            const getAllUsersLogic = new GetAllUsersLogic(search as string);
            const users = await getAllUsersLogic.execute();
            responseObj.data = { users };
            res.status(200).json(responseObj);
        } catch(e) {
            const status = e.status || 500;
            responseObj.message = e.message || "Internal server error";
            responseObj.meta.success = false;
            res.status(status).json(responseObj);
        }
    }
}