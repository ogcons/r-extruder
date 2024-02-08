import React from "react";
import {
  Upload,
  UploadOnAddEvent,
  UploadOnRemoveEvent,
} from "@progress/kendo-react-upload";
import { UploadInputProps } from "../../utils/types";
import "./UploadInput.scss";

const UploadInput: React.FC<UploadInputProps> = ({
  value,
  onChange,
  handleInsertFile,
}) => {
  const handleFileChange = (event: UploadOnAddEvent | UploadOnRemoveEvent) => {
    event.newState.forEach((file) => {
      if (!file.name.endsWith(".R")) {
        handleInsertFile(true);
      }
    });
    onChange({ value: event.newState });
  };

  return (
    <Upload
      autoUpload={false}
      showActionButtons={false}
      files={value}
      onAdd={handleFileChange}
      onRemove={handleFileChange}
    />
  );
};

export default UploadInput;
