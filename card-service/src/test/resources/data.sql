MERGE INTO card_status (card_status_name)
KEY (card_status_name)
VALUES
  ('VALID'),
  ( 'INVALID'),
  ( 'EXPIRED');

MERGE INTO batch_status (batch_status_name)
    KEY (batch_status_name)
    VALUES
        ('RECEIVED'),
        ('PROCESSED'),
        ('PARTIALLY_PROCESSED'),
        ('FAILED');
