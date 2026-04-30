# ERD (Mermaid)

```mermaid
erDiagram
  USERS {
    BIGINT id PK
    VARCHAR email
    VARCHAR username
    VARCHAR password_hash
    VARCHAR full_name
    VARCHAR phone_number
    VARCHAR avatar_url
    BOOLEAN gender
    DATE date_of_birth
    VARCHAR status
    TIMESTAMP created_at
    TIMESTAMP updated_at
  }

  ROLES {
    BIGINT id PK
    VARCHAR name
  }

  USER_ROLES {
    BIGINT user_id FK
    BIGINT role_id FK
  }

  ADDRESSES {
    BIGINT id PK
    BIGINT user_id FK
    VARCHAR phone_number
    VARCHAR street
    VARCHAR city
    VARCHAR state
    VARCHAR country
    VARCHAR zip_code
    VARCHAR description
    BOOLEAN is_default
  }

  REFRESH_TOKENS {
    BIGINT id PK
    VARCHAR token
    BIGINT user_id
    TIMESTAMP expiry_date
  }

  NOTIFICATIONS {
    BIGINT id PK
    BIGINT user_id
    VARCHAR title
    TEXT message
    VARCHAR type
    VARCHAR reference_id
    BOOLEAN is_read
    TIMESTAMP created_at
  }

  CATEGORIES {
    UUID id PK
    VARCHAR name
    VARCHAR description
    VARCHAR image_url
    TIMESTAMP created_at
    TIMESTAMP updated_at
  }

  PRODUCTS {
    UUID id PK
    VARCHAR name
    TEXT description
    DOUBLE price
    BOOLEAN is_active
    VARCHAR created_by
    UUID category_id FK
    TIMESTAMP created_date
    VARCHAR creator_name
    TIMESTAMP last_modified_date
  }

  VARIANTS {
    UUID id PK
    UUID product_id FK
    INT stock
    DOUBLE price
    VARCHAR image_url
  }

  PRODUCT_ATTRIBUTES {
    UUID id PK
    UUID variant_id FK
    VARCHAR type
    VARCHAR value
  }

  PRODUCT_IMAGES {
    UUID id PK
    UUID product_id FK
    VARCHAR image_url
  }

  CARTS {
    BIGINT user_id PK
    TIMESTAMP last_updated
  }

  CART_ITEMS {
    UUID id PK
    UUID variant_id FK
    BIGINT cart_id FK
    INT quantity
  }

  ORDERS {
    UUID id PK
    VARCHAR reference
    BIGINT user_id
    INT address_id
    DOUBLE total_amount
    DOUBLE shipping_fee
    VARCHAR payment_method
    VARCHAR status
    TEXT notes
    TIMESTAMP created_date
    TIMESTAMP last_modified_date
  }

  ORDER_ITEMS {
    UUID id PK
    UUID order_id FK
    UUID product_id
    UUID variant_id
    INT quantity
    DOUBLE price
  }

  REVIEWS {
    BIGINT id PK
    BIGINT user_id
    UUID product_id
    INT rating
    TEXT comment
    TIMESTAMP created_date
    TIMESTAMP last_modified_date
  }

  USERS ||--o{ ADDRESSES : has
  USERS ||--o{ USER_ROLES : has
  ROLES ||--o{ USER_ROLES : has
  USERS ||--o{ REFRESH_TOKENS : has
  USERS ||--o{ NOTIFICATIONS : receives
  USERS ||--o{ ORDERS : places
  USERS ||--o{ REVIEWS : writes

  CATEGORIES ||--o{ PRODUCTS : contains
  PRODUCTS ||--o{ VARIANTS : has
  VARIANTS ||--o{ PRODUCT_ATTRIBUTES : has
  PRODUCTS ||--o{ PRODUCT_IMAGES : has
  PRODUCTS ||--o{ REVIEWS : has

  USERS ||--o| CARTS : owns
  CARTS ||--o{ CART_ITEMS : has
  VARIANTS ||--o{ CART_ITEMS : in

  ORDERS ||--o{ ORDER_ITEMS : has
  VARIANTS ||--o{ ORDER_ITEMS : ordered_as
  ADDRESSES ||--o{ ORDERS : shipping_address
```

Notes:
1. Các quan hệ bằng `*_id` nhưng trong code đang để kiểu scalar (vd `orders.userId`, `orders.addressId`, `reviews.userId/productId`, `order_items.variantId/productId`, `notifications.userId`, `refresh_tokens.userId`) được thể hiện như “logical FK” trong ERD (vẽ theo ý nghĩa dữ liệu). Nó không đảm bảo DB đang có FK constraint thật.
2. `CartItem` đang dùng `@JoinColumn(name = "cart_id")` trỏ sang `Cart` có PK field là `userId`. Với naming strategy mặc định của Spring (camelCase -> snake_case), cột PK của `carts` thường là `user_id`, nên bạn có thể muốn đổi join column sang `user_id` (hoặc đặt `@Column(name = "cart_id")` cho `Cart.userId`) để tránh lệch tên cột.
