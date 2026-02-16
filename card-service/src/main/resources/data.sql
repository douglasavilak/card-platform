INSERT IGNORE INTO card_status (card_status_id, card_status_name) VALUES
                                                               (1, 'VALID'),
                                                               (2, 'INVALID'),
                                                               (3, 'EXPIRED');

INSERT IGNORE INTO batch_status (batch_status_id, batch_status_name) VALUES
                                                                         (1, 'RECEIVED'),
                                                                         (2, 'PROCESSED'),
                                                                         (3, 'PARTIALLY_PROCESSED'),
                                                                         (4, 'FAILED');