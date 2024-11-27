export interface ILogic<T> {
    execute(params?: any): Promise<T> | T;
}