import React from "react";
import {
  Upload,
  UploadOnAddEvent,
  UploadOnRemoveEvent,
  ExternalDropZone,
} from "@progress/kendo-react-upload";
import { UploadInputProps } from "../../utils/types";
import "./UploadInput.scss";

const uploadRef = React.createRef<Upload>();

const UploadInput: React.FC<UploadInputProps> = ({
  value,
  onChange,
  handleInsertFile,
}) => {
  const handleFileChange = (event: UploadOnAddEvent | UploadOnRemoveEvent) => {
    event.newState.forEach((file) => {
      if (!file.name.endsWith(".R")) {
        handleInsertFile(true);
      } else {
        handleInsertFile(false);
      }
    });
    onChange({ value: event.newState });
  };

  return (
    <>
      <ExternalDropZone
        uploadRef={uploadRef}
        style={{ borderBlockColor: "#3498db", borderColor: "#3498db" }}
      />
      <div id={"upload"} className="upload">
        <Upload
          ref={uploadRef}
          autoUpload={false}
          showActionButtons={false}
          files={value}
          onAdd={handleFileChange}
          onRemove={handleFileChange}
        />
      </div>
    </>
  );
};

export default UploadInput;
