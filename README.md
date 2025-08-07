# Payment End-to-End Automation Framework

This repository contains an automated testing framework for end-to-end testing of the booking and payment flow. It is built using Java, Selenium, Rest Assured, and TestNG.

## Features

- End-to-End flow: from selecting a time slot to completing a payment.
- Supports testing multiple payment gateways:
  - Stripe
  - Paymob
- Coupon support:
  - Full discount (100%)
  - Partial discount
- Wallet usage scenarios
- API + UI hybrid automation

> **Note:** Fawry scenarios are not implemented yet.

## Project Structure

- `src/test/java`: Contains the test classes and scenarios.
- `src/main/java`: Contains the core framework logic and utilities.
- `resources/environment.properties`: Environment-specific configurations.

## Setup Instructions

1. **Clone the repository:**

```bash
git clone https://github.com/BassemShezlong/payment-end-to-end.git
cd payment-end-to-end
```

2. **Import into IntelliJ IDEA or any Java IDE as a Maven Project.**

3. **Update configuration if needed:**
   - Modify environment details in `resources/environment.properties`

4. **Run the tests:**
   - Right-click on the test class or use TestNG configuration.
   - You can also use Maven to run tests from the terminal:
     ```bash
     mvn clean test
     ```

## Requirements

- Java 11+
- Maven 3.6+
- Internet access for testing payment redirections
- Chrome browser (latest)

## Author

[Bassem Galal](https://www.linkedin.com/in/basem-galal-97aa4b191)

---

Feel free to fork or raise an issue for contributions.
