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

import * as net from "net";
import {ExtensionContext, extensions, StatusBarAlignment, window} from "vscode";
import {
    Executable,
    LanguageClient,
    LanguageClientOptions,
} from "vscode-languageclient/lib/main";

export function activate(context: ExtensionContext) {
    const fs = require("fs");

    // path resolved to identify the location of the LSP server into the extension
    const extPath = extensions.getExtension("BroadcomMFD.cobol-language-support").extensionPath;
    const LSPServerPath = `${extPath}/server/lsp-service-cobol-0.8.0.jar`;

    let serverOptions: Executable;

    try {
        if (fs.existsSync(LSPServerPath)) {
            serverOptions = {
                args: ["-Dline.separator=\r\n", "-jar", LSPServerPath, "pipeEnabled"],
                command: "java",
                options: { stdio: "pipe", detached: false },
            };
        } else {
            window.showErrorMessage("Cobol extension failed to start - LSP server not found");
            return;
        }
    } catch (err) {
        window.showErrorMessage(err);
        return;
    }

    // Options to control the language client
    const clientOptions: LanguageClientOptions = {
        // Register the server for cobol
        documentSelector: ["COBOL"],
    };
    const item = window.createStatusBarItem(StatusBarAlignment.Right, Number.MIN_VALUE);

    // Create the language client and start the client.
    const languageClient = new LanguageClient("LSP", "LSP extension for Cobol language", serverOptions, clientOptions);

    const disposable = languageClient.start();

    context.subscriptions.push(disposable);
    item.text = "Language Server is active";
}
