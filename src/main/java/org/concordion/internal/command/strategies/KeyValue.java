package org.concordion.internal.command.strategies;

import org.concordion.api.*;
import org.concordion.api.listener.VerifyRowsListener;
import org.concordion.internal.Row;
import org.concordion.internal.SummarizingResultRecorder;
import org.concordion.internal.util.Announcer;

public class KeyValue extends AbstractChangingOrderVerifyRowsStrategy {

    public KeyValue(CommandCall commandCall, Evaluator evaluator, ResultRecorder resultRecorder,
                    Announcer<VerifyRowsListener> listeners, String loopVariableName, Iterable<Object> actualRows) {
        super(commandCall, evaluator, resultRecorder, listeners, loopVariableName, actualRows);
    }

    @Override
    protected Object findMatchingRow(Row expectedRow) {

        Element[] headerCells = tableSupport.getLastHeaderRow().getCells();
        CommandCallList childrenCalls = commandCall.getChildren();

        assert headerCells.length == childrenCalls.size();

        SummarizingResultRecorder backgroundResultRecorder = new SummarizingResultRecorder();
        for (Object row : actualRows) {

            tableSupport.copyCommandCallsTo(expectedRow.deepClone());
            evaluator.setVariable(loopVariableName, row);

            long total = 0;
            long success = 0;

            for (int column = 0; column < headerCells.length; column++) {
                childrenCalls.get(column).verify(evaluator, backgroundResultRecorder);

                String matchingRole = headerCells[column].getConcordionAttributeValue("matchingRole", "matching-role");
                if (matchingRole != null && matchingRole.equalsIgnoreCase("key")) {
                    total += backgroundResultRecorder.getTotalCount();
                    success += backgroundResultRecorder.getSuccessCount();
                }

                backgroundResultRecorder.reset();
            }

            if (total == 0) {
                throw new RuntimeException("KeyValue strategy expects at least one column marked as matchingRole=\"key\". Key must be unique in expected table");
            }

            if (total == success) {
                return row;
            }
        }
        return null;
    }
}
