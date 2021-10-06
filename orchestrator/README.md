# Stanbic Bank Core Banking Bridge

Below are the **REQUIRED** infrastructure needed to ensure the service runs correctly.

  - A **JAVA** Runtime Environment (JRE)
  - Required **Environment Variables** on your deployment environment (See below for more information)


# Environment Variables

Kindly ensure you have the following variables in your environment.

| Variable Key| Example Value|
| ------ | ------ |
| ENVIRONMEMT | Local |
| CBA_BRIDGE_SERVICE_PORT | 8632 |
| CBA_SOURCE_CODE | ICOLLECT |
| CBA_SOURCE_CHANNEL_ID | ICOLLECT |
| CBA_REQUEST_PASSWORD | Icollectmaster21 |
| CBA_ACCOUNT_LOOKUP_HOST_ROOT | https://sandboxapis.stanbic.com.gh:448 |
| CBA_ACCOUNT_LOOKUP_PATH | /stanbicghana/internal/AccountServices/accounts/ |
| CBA_FUNDSTRANSFER_HOST_ROOT | https://run.mocky.io:443 |
| CBA_FUNDSTRANSFER_PATH | /v3/1d52b480-d9aa-43a8-85b9-8fa028b81f57 |

