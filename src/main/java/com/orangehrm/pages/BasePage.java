package com.orangehrm.pages;

import java.util.Objects;

import org.openqa.selenium.WebDriver;

import com.orangehrm.actiondriver.ActionDriver;

public abstract class BasePage {

	protected final WebDriver driver;
	protected final ActionDriver actionDriver;

	protected BasePage(WebDriver driver) {
		this.driver = Objects.requireNonNull(driver, "driver");
		this.actionDriver = new ActionDriver(this.driver);
	}
}
