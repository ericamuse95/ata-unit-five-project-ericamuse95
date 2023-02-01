#!/bin/bash
set -eo pipefail
TEMPLATE=ReferralService-template.yml

source ./setupEnvironment.sh

if [ -z "$GITHUB_USERNAME" ] || [ "$GITHUB_USERNAME" == "yourusernameinlowercase" ] ; then
  echo "Your environment variable GITHUB_USERNAME is not properly configured.  Make sure that you have set it properly in setupEnvironment.sh"
  exit 1
fi

./gradlew :ReferralServiceLambda:build -i

echo "Deleting Application UNIT_FIVE_APPLICATION_STACK"
echo "This may take 15-20 minutes...  But if takes more than 25 minutes then it may have failed. Check your CloudFormation Stack on the AWS UI for errors."
aws cloudformation delete-stack --stack-name $UNIT_FIVE_SERVICE_STACK_DEV
aws cloudformation wait stack-delete-complete --stack-name $UNIT_FIVE_SERVICE_STACK_DEV

aws cloudformation package --template-file $TEMPLATE --s3-bucket $UNIT_FIVE_ARTIFACT_BUCKET --output-template-file referral-service-development.yml
aws cloudformation deploy --template-file referral-service-development.yml --stack-name $UNIT_FIVE_SERVICE_STACK_DEV --capabilities CAPABILITY_NAMED_IAM
