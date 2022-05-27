package com.etshost.msu.bean;

import org.flywaydb.core.Flyway;

public class Migration extends Flyway  {

    public void repairAndMigrate() {
        this.repair();
        this.migrate();
    }
    
}
