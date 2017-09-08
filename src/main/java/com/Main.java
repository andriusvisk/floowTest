package com;

public class Main {

    public static void main(String[] args) {
        Parameters parameters = new Parameters(args);

        DbUtils dbUtils = new DbUtils(parameters);
        dbUtils.test();
        //

    }
}
