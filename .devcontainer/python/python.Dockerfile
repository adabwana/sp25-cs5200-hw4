FROM python:3.11-slim

# Install system dependencies
RUN apt-get update && apt-get install -y \
    git \
    curl \
    graphviz \
    graphviz-dev \
    build-essential \
    pkg-config \
    && rm -rf /var/lib/apt/lists/*

# Create non-root user
ARG USERNAME=vscode
ARG USER_UID=1000
ARG USER_GID=$USER_UID

RUN groupadd --gid $USER_GID $USERNAME \
    && useradd --uid $USER_UID --gid $USER_GID -m $USERNAME

# Install Python packages
COPY pyquirements.txt /tmp/pyquirements.txt
RUN pip install --no-cache-dir -r /tmp/pyquirements.txt

USER $USERNAME

WORKDIR /workspaces/04-uniformed-search

# # Add NVIDIA GPU support
# COMMENT IF YOU DONT HAVE A GPU
# ENV NVIDIA_VISIBLE_DEVICES=all
# ENV NVIDIA_DRIVER_CAPABILITIES=compute,utility

