package com.joaomsa.tp1;

public class Person {
    public String name;
    public String email;
    public String phone;

    public boolean validateName()
    {
        return name != null && !name.isEmpty();
    }

    public boolean validateEmail()
    {
        return email != null && !email.isEmpty();
    }

    public boolean validatePhone()
    {
        return phone != null && !phone.isEmpty();
    }
}

