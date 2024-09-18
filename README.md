# Investment Profit Calculation Assignment

## Objective
The objective of this assignment is to assess your ability to work with financial data, specifically transactions involving securities, and to calculate their performance metrics.

## Task
You are required to implement a solution that processes a set of transaction records for a portfolio of investments. The primary goal is to calculate the profitability of the portfolio.

## Requirements

### Input Data
Each record will include the following details:
- Quantity
- Price
- Transaction type (buy/sell)
- Timestamp
- Transaction fee

### Example Data:
```json
[
  {"type": "buy", "quantity": 100, "price": 10.00, "fee": 2.00, "timestamp": "2024-01-01T10:00:00Z"},
  {"type": "buy", "quantity": 50, "price": 12.00, "fee": 1.00, "timestamp": "2024-02-01T11:30:00Z"},
  {"type": "sell", "quantity": 70, "price": 15.00, "fee": 1.25, "timestamp": "2024-03-01T14:00:00Z"}
]
```

**NB!** You are required to generate this input data yourself. The data should be diverse and realistic, including various buy and sell transactions with different quantities and prices. The dataset size should be at least **1000 rows**.

### Profitability Calculation
You are required to calculate the profitability of the portfolio based on the provided transactions. You have the flexibility to choose the formula you believe is most appropriate for calculating profit or loss.

---

## Advanced Features (Optional)

### Advanced Level I: Transaction Fees
Transaction fees should be deducted from the overall profitability, reducing the total profit or increasing the total loss.

**Example:**  
If a transaction fee of $2.00 per trade is applied, this amount should be subtracted from the calculated profit for each transaction.

### Advanced Level II: Dividend Payments
Extend the calculation to include dividend payments. Dividends should be treated as additional profits and added to the overall profitability.

**Example:**  
If a dividend of $1.00 per unit is paid on the 150 units bought before the sale, it should increase the total profit.

### Advanced Level III: Portfolio Analytics at LHV Pank
Conduct an analysis of the portfolio analytics at LHV Pank. Provide insights into what is working well in the current analytics setup and identify any gaps or areas that could be improved.

**Suggestions:**  
- Analyze how well the current system tracks performance metrics.
- Identify missing data points or suggest enhancements to existing tools.

---

## Technical Requirements

- **Language/Tech Stack:** You are free to use any programming language, framework, or libraries you feel comfortable with.
- **Testing:** Provide unit tests for your solution.
- **Codebase:** Ensure the codebase is clear, well-structured, and maintainable.

---

## Submission Guidelines

Submit your solution via a Git repository link. Ensure the repository is public or accessible by our team.

---

## Evaluation Criteria

1. Correctness of the solution.
2. Code quality, including readability and maintainability.
3. Testing coverage.
4. Handling of edge cases and optional advanced features.
