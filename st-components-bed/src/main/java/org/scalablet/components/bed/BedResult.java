package org.scalablet.components.bed;

/**
 * Executor execution result
 *
 * @author abomb4 2021-10-13 21:43:54
 */
public class BedResult {

    /** Execution result type */
    private final EnumBedResultType resultType;
    /** If not finished, executor can provide a simple message shorter than 255 bytes */
    private final String message;

    /**
     * Build a completed result
     *
     * @return completed result
     */
    public static BedResult complete() {
        return new BedResult(EnumBedResultType.COMPLETED, null);
    }

    /**
     * Build a incomplete result
     *
     * @return incomplete result
     */
    public static BedResult incomplete(String message) {
        return new BedResult(EnumBedResultType.INCOMPLETE, message);
    }

    /**
     * Build a unrecognized result
     *
     * @return unrecognized result
     */
    public static BedResult unrecognized(String message) {
        return new BedResult(EnumBedResultType.UNRECOGNIZED, message);
    }

    /**
     * Construct full BedResult
     *
     * @param resultType Means completed or incomplete
     * @param message    Simple message limited by 255 characters
     */
    private BedResult(EnumBedResultType resultType, String message) {
        this.resultType = resultType;
        this.message = message;
    }

    /**
     * completed
     *
     * @return is completed
     */
    public boolean isCompleted() {
        return resultType == EnumBedResultType.COMPLETED;
    }

    /**
     * Get execution result type
     *
     * @return Execution result type
     */
    public EnumBedResultType getResultType() {
        return resultType;
    }

    /**
     * Get message
     *
     * @return message
     */
    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return resultType + "(" + message + ")";
    }
}
