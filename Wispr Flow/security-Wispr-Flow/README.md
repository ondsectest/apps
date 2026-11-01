# Security, data and privacy findings for Wispr Flow

Security, data-handling and privacy findings from testing Wispr Flow, kept together regardless of platform. Each folder is a working prototype of the issue and a proposed fix, built with mock or redacted data only.

| Finding | What it covers |
| --- | --- |
| [Deleting Dictionary Words and Snippets](./Deleting%20Dictionary%20Words%20and%20Snippets) | Deleted dictionary words and snippets are still returned by the server with their full text and a deleted flag, long after deletion, and the app states no retention period. The fix confirms honestly, shows a Recently deleted countdown, and erases on a clear schedule. |
| [Personal Dictionary Word Length Validation](./Personal%20Dictionary%20Word%20Length%20Validation) | The desktop Add word field caps input at 60 characters, but the Dictionary Personal API accepts and stores a 100-character word when the client cap is bypassed. Length is validated only on the client. The fix enforces the limit server-side and returns a clear validation error. |

Some findings in this area are reported to Wispr privately rather than published. Those are not included here.

Findings by Santhosh Tuppad.
