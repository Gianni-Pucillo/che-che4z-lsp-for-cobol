/*
 * Copyright (c) 2019 Broadcom.
 * The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Broadcom, Inc. - initial API and implementation
 */
package com.ca.lsp.cobol.service.delegates.completions;

import com.ca.lsp.cobol.service.mocks.TestLanguageClient;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.ca.lsp.cobol.usecases.UseCaseUtils.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * This tests checks the order of completion elements: 1. Variables 2. Keywords
 *
 * @author teman02
 */
public class CompletionsChainTest {
  private static final String TEXT =
      "       Identification Division. \n"
          + "       Program-id.    ProgramId.\n"
          + "       Data Division.\n"
          + "       Working-Storage Section.\n"
          + "       01   Feedback. \n"
          + "        02   Fb-severity      PIC 9(4) Binary. \n"
          + "        02   Ab-detail        PIC X(10).\n"
          + "       77   Dest-output       PIC S9(9) Binary.\n"
          + "       77   Lildate           PIC S9(9) Binary.\n"
          + "       77   Lilsecs           COMP-2.\n"
          + "       77   Greg              PIC X(17).\n"
          + "       01   Pattern.\n"
          + "        02                    PIC 9(4) Binary Value 45.\n"
          + "        02                    PIC X(45) Value \"\".\n"
          + "       77   Start-Msg         PIC X(80) Value \"\".\n"
          + "       77   Ending-Msg        PIC X(80) Value \"\".\n"
          + "       01 Msg.\n"
          + "         02 Stringlen         PIC S9(4) Binary.\n"
          + "         02 Str               .\n"
          + "          03                  PIC X Occurs 1 to 80 times\n"
          + "                                     Depending on Stringlen.\n"
          + "       Procedure Division.\n"
          + "       000-Main-Logic.\n"
          + "           Perform 100-Say-Hello.\n"
          + "           Perform 200-Get-Date.\n"
          + "           Perform 300-Say-Goodbye.\n"
          + "           Stop Run.\n"
          + "       100-Say-Hello. f\n"
          + "       200-Get-Date. A\n"
          + "       300-Say-Goodbye.\n"
          + "       End program ProgramId.";

  @Test
  public void testCompletionsOrder() throws ExecutionException, InterruptedException {
    TextDocumentService service = runServer();

    List<CompletionItem> list = getCompletionItems(service, new Position(28, 22));
    assertFalse(list.isEmpty());

    assertEquals(CompletionItemKind.Variable, list.get(0).getKind());
    assertEquals(CompletionItemKind.Snippet, list.get(1).getKind());
    assertEquals(CompletionItemKind.Keyword, list.get(list.size()-1).getKind());
  }

  private List<CompletionItem> getCompletionItems(TextDocumentService service, Position position)
      throws InterruptedException, ExecutionException {
    CompletableFuture<Either<List<CompletionItem>, CompletionList>> completions =
        service.completion(new CompletionParams(new TextDocumentIdentifier("1"), position));

    Either<List<CompletionItem>, CompletionList> either = completions.get();
    return either.getRight().getItems();
  }

  private TextDocumentService runServer() {
    TestLanguageClient client = new TestLanguageClient();
    TextDocumentService service = createServer(client);

    runTextValidation(service, TEXT);
    waitForDiagnostics(client);
    return service;
  }
}
