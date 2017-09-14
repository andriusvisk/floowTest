package com.charting;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by andrius on 22/10/2016.
 */
public class BarColor {

    // 0 - back
    // 1 - border
    public List<String> getColor(int which) {
        List<String> ret = new ArrayList();
        switch (which) {
            case 0:
                ret.add("'rgba(255, 99, 132, 0.2)'");
                ret.add("'rgba(255,99,132,1)'");
                break;
            case 1:
                ret.add("'rgba(54, 162, 235, 0.2)'");
                ret.add("'rgba(54, 162, 235, 1)'");
                break;
            case 2:
                ret.add("'rgba(255, 206, 86, 0.2)'");
                ret.add("'rgba(255, 206, 86, 1)'");
                break;
            case 3:
                ret.add("'rgba(75, 192, 192, 0.2)'");
                ret.add("'rgba(75, 192, 192, 1)'");
                break;
            case 4:
                ret.add("'rgba(153, 102, 255, 0.2)'");
                ret.add("'rgba(153, 102, 255, 1)'");
                break;
            case 5:
                ret.add("'rgba(255, 159, 64, 0.2)'");
                ret.add("'rgba(255, 159, 64, 1)'");
                break;

        }
        return ret;
    }

    public String[] getRandomColorsForChart(int countObj) {
        BarColor barColor = new BarColor();
        String backColors = "";
        String borderColors = "";
        while(--countObj >= 0) {
            if (backColors.length() > 0) {
                backColors = backColors + ", ";
                borderColors = borderColors + ", ";
            }
            List color = barColor.getRandomColor();
            backColors = backColors + color.get(0);
            borderColors = borderColors + color.get(1);
        }
        return new String[]{backColors,borderColors};
    }

    public List getRandomColor(){
        return getColor(ThreadLocalRandom.current().nextInt(0, 5 + 1));
    }
}
