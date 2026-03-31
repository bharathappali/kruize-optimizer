#!/bin/bash
set -e

# Usage function
usage() {
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  -i IMAGE_NAME    Full image name (registry/repository:tag)"
    echo "  -t TAG           Image tag (default: 0.1) - used only if -i is not provided"
    echo "  -b BUILD         Build image true/false (default: true)"
    echo "  -p PUSH          Push image true/false (default: true)"
    echo "  -h               Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 -i quay.io/myuser/kruize-optimizer:1.0.0 -b true -p true"
    echo "  $0 -t 0.1_mvp -p false"
    echo "  $0 -b true -p true"
    echo ""
    echo "Note: Use either -i for full image name OR -t for tag with default registry/repo"
    exit 1
}

# Default values
IMAGE_TAG="0.1"
QUARKUS_BUILD="true"
QUARKUS_PUSH="true"
IMAGE_NAME=""

# Parse command line arguments
while getopts "i:t:b:p:h" opt; do
    case ${opt} in
        i )
            IMAGE_NAME=$OPTARG
            ;;
        t )
            IMAGE_TAG=$OPTARG
            ;;
        b )
            QUARKUS_BUILD=$OPTARG
            ;;
        p )
            QUARKUS_PUSH=$OPTARG
            ;;
        h )
            usage
            ;;
        \? )
            echo "Invalid option: -$OPTARG" 1>&2
            usage
            ;;
    esac
done

# If IMAGE_NAME is not provided via -i, use default registry/repository with tag
if [ -z "$IMAGE_NAME" ]; then
    IMAGE_NAME="quay.io/kruize/optimizer:${IMAGE_TAG}"
fi

QUARKUS_PLATFORMS="linux/amd64,linux/arm64"

echo "Building and pushing image: ${IMAGE_NAME}..."
echo "Build: ${QUARKUS_BUILD}"
echo "Push: ${QUARKUS_PUSH}"
echo "Platforms: ${QUARKUS_PLATFORMS}"

# Build container image using Quarkus Jib extension
./mvnw clean package \
  -Dquarkus.container-image.build=${QUARKUS_BUILD} \
  -Dquarkus.container-image.image=${IMAGE_NAME} \
  -Dquarkus.container-image.push=${QUARKUS_PUSH} \
  -Dquarkus.jib.platforms=${QUARKUS_PLATFORMS}