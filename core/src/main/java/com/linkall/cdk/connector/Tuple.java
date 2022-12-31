package com.linkall.cdk.connector;

import java.util.LinkedList;
import java.util.List;

public class Tuple {
    private final List<Element> elements = new LinkedList<>();

    private SuccessCallback success;

    private FailedCallback failed;

    public Tuple() {
    }

    public Tuple(Element ele) {
        this.elements.add(ele);
    }

    public Tuple(Element ele, SuccessCallback success, FailedCallback failed) {
        this.elements.add(ele);
        this.success = success;
        this.failed = failed;
    }

    public List<Element> getElements() {
        return this.elements;
    }

    public SuccessCallback getSuccess() {
        return success;
    }

    public void setSuccess(SuccessCallback success) {
        this.success = success;
    }

    public FailedCallback getFailed() {
        return failed;
    }

    public void setFailed(FailedCallback failed) {
        this.failed = failed;
    }

    public void addElement(Element element) {
        this.elements.add(element);
    }
}

