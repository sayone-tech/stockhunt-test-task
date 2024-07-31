package com.task.tradingAutomation.exception;

public class UserDefinedExceptions {



    public static class InvalidPayloadException extends RuntimeException {
        public InvalidPayloadException(String message) {
            super(message);
        }
    }

    public static class TradeNotAllowedException extends RuntimeException {
        public TradeNotAllowedException(String message) {
            super(message);
        }
    }

    public static class TradeNotPlacedException extends RuntimeException {
        public TradeNotPlacedException(String message) {
            super(message);
        }
    }

    public static class MarketDataRetrievalException extends RuntimeException {
        public MarketDataRetrievalException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
