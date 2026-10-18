package com.hotel.service.checkout;

public interface CheckOutService {
  CheckOutResult previewCheckOut(int roomNumber);

  CheckOutResult checkOut(int roomNumber);
}
