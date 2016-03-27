package com.seniordesigndbgt.dashboardtest.modeltests;


import org.junit.Test;

import static org.junit.Assert.*;
import com.seniordesigndbgt.dashboard.model.StockHistory;

/**
 * Created by kamehardy on 3/27/16.
 */
public class StockHistoryTest {

    @Test
    public void testGetDateStock() throws Exception {
        String str = "2016-03-27";
        StockHistory history = new StockHistory();
        history.setDateStock(str);
        assertEquals(str, history.getDateStock());
    }

    @Test
    public void testGetClosePrice() throws Exception {
        Double val = 18.30;
        StockHistory his = new StockHistory();
        his.setClosePrice(val);
        assertEquals(val, his.getClosePrice(), 0.0);
    }

}