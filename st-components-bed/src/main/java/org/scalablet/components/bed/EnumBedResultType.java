package org.scalablet.components.bed;

/**
 * Result type
 *
 * @author abomb4 2021-10-13 21:44:42
 */
public enum EnumBedResultType {
    /** Already completed */
    COMPLETED,
    /** Incomplete, the dispatcher will invoke executor again */
    INCOMPLETE,
    /** Unrecognized task, it may not able to execute later */
    UNRECOGNIZED,
    ;
}
