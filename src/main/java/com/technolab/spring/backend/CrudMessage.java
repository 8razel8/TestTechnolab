package com.technolab.spring.backend;

public class CrudMessage {
    public enum DML {
        INSERT,
        UPDATE,
        DELETE;
    }
    private DML operation;
    private Object object;

    public CrudMessage(DML operation, Object object) {
        this.operation = operation;
        this.object = object;
    }

    public DML getOperation() {
        return operation;
    }

    public void setOperation(DML operation) {
        this.operation = operation;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }
}
