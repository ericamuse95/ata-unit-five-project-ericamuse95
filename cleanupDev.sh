#!/bin/bash
set -eo pipefail

source ./setupEnvironment.sh

if [ -z "$GITHUB_USERNAME" ] || [ "$GITHUB_USERNAME" == "yourusernameinlowercase" ] ; then
  echo "Your environment variable GITHUB_USERNAME is not properly configured.  Make sure that you have set it properly in setupEnvironment.sh"
  exit 1
fi

echo "Deleting Application $UNIT_FIVE_SERVICE_STACK_DEV"
echo "This may take 20-25 minutes...  But if takes more than 30 minutes then it may have failed. Check your CloudFormation Stack on the AWS UI for errors."
aws cloudformation delete-stack --stack-name $UNIT_FIVE_SERVICE_STACK_DEV
aws cloudformation wait stack-delete-complete --stack-name $UNIT_FIVE_SERVICE_STACK_DEV
