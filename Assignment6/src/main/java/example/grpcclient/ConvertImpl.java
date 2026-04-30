package example.grpcclient;

import io.grpc.stub.StreamObserver;
import service.*;

/**
 * ConverterImpl
 *
 * Implements the gRPC Converter service.
 * Supports conversions between:
 * - Length (km, mile, yard, foot)
 * - Weight (kg, pound)
 * - Temperature (Celsius, Fahrenheit)
 *
 * Includes full validation, unit compatibility checks,
 * and error handling as required by the assignment.
 *
 * Author: Mamoudou
 * Version: 1.1
 * Date: 2026-04-30
 */
class ConverterImpl extends ConverterGrpc.ConverterImplBase {

    public ConverterImpl() {
        super();
    }

    @Override
    public void convert(ConversionRequest req, StreamObserver<ConversionResponse> responseObserver) {

        System.out.println("Received conversion request: " + req);

        ConversionResponse.Builder response = ConversionResponse.newBuilder();

        double value = req.getValue();
        String from = req.getFromUnit() == null ? "" : req.getFromUnit().trim().toUpperCase();
        String to = req.getToUnit() == null ? "" : req.getToUnit().trim().toUpperCase();

        // -------------------------
        // VALIDATION
        // -------------------------

        if (from.isEmpty()) {
            response.setIsSuccess(false)
                    .setResult(0)
                    .setError("from_unit is required");
            responseObserver.onNext(response.build());
            responseObserver.onCompleted();
            return;
        }

        if (to.isEmpty()) {
            response.setIsSuccess(false)
                    .setResult(0)
                    .setError("to_unit is required");
            responseObserver.onNext(response.build());
            responseObserver.onCompleted();
            return;
        }

        if (from.equals(to)) {
            response.setIsSuccess(false)
                    .setResult(value)
                    .setError("same unit - no conversion needed");
            responseObserver.onNext(response.build());
            responseObserver.onCompleted();
            return;
        }

        double result;

        try {

            // -------------------------
            // LENGTH
            // -------------------------
            if (isLength(from) && isLength(to)) {

                double meters = toMeters(value, from);
                result = fromMeters(meters, to);
            }

            // -------------------------
            // WEIGHT
            // -------------------------
            else if (isWeight(from) && isWeight(to)) {

                if (from.equals("KILOGRAM") && to.equals("POUND")) {
                    result = value * 2.20462;
                } else if (from.equals("POUND") && to.equals("KILOGRAM")) {
                    result = value / 2.20462;
                } else {
                    response.setIsSuccess(false)
                            .setResult(0)
                            .setError("unsupported unit: " + from);
                    responseObserver.onNext(response.build());
                    responseObserver.onCompleted();
                    return;
                }
            }

            // -------------------------
            // TEMPERATURE
            // -------------------------
            else if (isTemperature(from) && isTemperature(to)) {

                // absolute zero checks
                if (from.equals("CELSIUS") && value < -273.15) {
                    response.setIsSuccess(false)
                            .setResult(0)
                            .setError("temperature below absolute zero (−273.15°C or −459.67°F)");
                    responseObserver.onNext(response.build());
                    responseObserver.onCompleted();
                    return;
                }

                if (from.equals("FAHRENHEIT") && value < -459.67) {
                    response.setIsSuccess(false)
                            .setResult(0)
                            .setError("temperature below absolute zero (−273.15°C or −459.67°F)");
                    responseObserver.onNext(response.build());
                    responseObserver.onCompleted();
                    return;
                }

                // conversions
                if (from.equals("CELSIUS") && to.equals("FAHRENHEIT")) {
                    result = (value * 9 / 5) + 32;
                } else if (from.equals("FAHRENHEIT") && to.equals("CELSIUS")) {
                    result = (value - 32) * 5 / 9;
                } else {
                    response.setIsSuccess(false)
                            .setResult(0)
                            .setError("unsupported unit: " + from + " to " + to);
                    responseObserver.onNext(response.build());
                    responseObserver.onCompleted();
                    return;
                }
            }

            // -------------------------
            // INVALID COMBINATION
            // -------------------------
            else {
                response.setIsSuccess(false)
                        .setResult(0)
                        .setError("units do not match - cannot convert " + from + " to " + to);

                responseObserver.onNext(response.build());
                responseObserver.onCompleted();
                return;
            }

            // -------------------------
            // SUCCESS RESPONSE
            // -------------------------
            response.setIsSuccess(true)
                    .setResult(result)
                    .setError("");

            responseObserver.onNext(response.build());
            responseObserver.onCompleted();

        } catch (Exception e) {
            response.setIsSuccess(false)
                    .setResult(0)
                    .setError("conversion error: " + e.getMessage());

            responseObserver.onNext(response.build());
            responseObserver.onCompleted();
        }
    }

    // -------------------------
    // HELPERS
    // -------------------------
    private boolean isLength(String u) {
        return u.equals("KILOMETER") || u.equals("MILE") || u.equals("YARD") || u.equals("FOOT");
    }

    private boolean isWeight(String u) {
        return u.equals("KILOGRAM") || u.equals("POUND");
    }

    private boolean isTemperature(String u) {
        return u.equals("CELSIUS") || u.equals("FAHRENHEIT");
    }

    private double toMeters(double value, String unit) {
        switch (unit) {
            case "KILOMETER":
                return value * 1000;
            case "MILE":
                return value * 1609.34;
            case "YARD":
                return value * 0.9144;
            case "FOOT":
                return value * 0.3048;
            default:
                return value;
        }
    }

    private double fromMeters(double meters, String unit) {
        switch (unit) {
            case "KILOMETER":
                return meters / 1000;
            case "MILE":
                return meters / 1609.34;
            case "YARD":
                return meters / 0.9144;
            case "FOOT":
                return meters / 0.3048;
            default:
                return meters;
        }
    }
}