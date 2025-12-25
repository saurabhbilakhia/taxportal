## Order:
1.	Create order: POST /api/v1/orders
2.	Upload documents to order: POST /api/v1/orders/{order_id}/documents (multipart with files)

    Headers: Authorization: Bearer <token>
    Form-data: file (pdf), slip_type (optional)
    Response: document record with id

3.	Create Checkout session POST /api/v1/orders/{order_id}/pay
4.	List orders: GET  /api/v1/orders?status=...

    Order status: Open -> Submitted → In Review → Pending Approval → Filed
    
5.	Get order details: GET  /api/v1/orders/{order_id}