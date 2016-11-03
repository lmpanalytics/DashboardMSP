/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tetrapak.dashboard.beans;

import com.tetrapak.dashboard.model.Invoice;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;

/**
 * This bean makes the sales logic.
 *
 * @author SEPALMM
 */
@Named(value = "salesBean")
@RequestScoped
public class SalesBean {

    private List<Invoice> salesList;

    /**
     * Creates a new instance of salesBean
     */
    public SalesBean() {
    }

    @PostConstruct
    public void init() {
        this.salesList = makeSales();
    }

    private List<Invoice> makeSales() {
        List<Invoice> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            list.add(new Invoice("mat_" + i, 100d * i));
        }
        return list;
    }

    public List<Invoice> getSalesList() {
        return salesList;
    }

    public void setSalesList(List<Invoice> salesList) {
        this.salesList = salesList;
    }

}
